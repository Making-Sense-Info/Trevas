package fr.insee.vtl.csv;

import fr.insee.vtl.model.Dataset;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.threeten.extra.Interval;

public class CSVDataset implements Dataset {

  private final DataStructure structure;
  private final CsvMapReader csvReader;
  private ArrayList<DataPoint> data;

  public CSVDataset(DataStructure structure, Reader csv) throws IOException {
    this(structure, csv, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
  }

  public CSVDataset(DataStructure structure, Reader csv, CsvPreference csvPreference)
      throws IOException {
    this.structure = structure;
    this.csvReader = new CsvMapReader(csv, csvPreference);
    var columns = this.csvReader.getHeader(true);
    if (!this.structure.keySet().containsAll(List.of(columns))) {
      throw new RuntimeException("missing columns in CSV");
    }
  }

  private CellProcessor[] getProcessors() {
    List<CellProcessor> processors = new ArrayList<>();
    for (String name : this.getColumnNames()) {
      // Find a valid processor for each type.
      processors.add(getProcessor(this.structure.get(name).getType()));
    }
    return processors.toArray(new CellProcessor[] {});
  }

  private CellProcessor getProcessor(Class<?> type) {
    if (String.class.equals(type)) {
      return new Optional();
    } else if (Long.class.equals(type)) {
      return new Optional(new ParseLong());
    } else if (Double.class.equals(type)) {
      return new Optional(new ParseDouble());
    } else if (Boolean.class.equals(type)) {
      return new Optional(new ParseBool());
    } else if (Instant.class.equals(type)) {
      throw new RuntimeException("TODO");
    } else if (LocalDate.class.equals(type)) {
      throw new RuntimeException("TODO");
    } else if (Interval.class.equals(type)) {
      // TODO: refine
      return new Optional();
    } else {
      throw new UnsupportedOperationException("unsupported type " + type);
    }
  }

  private String[] getNameMapping() {
    return this.getColumnNames().toArray(new String[] {});
  }

  @Override
  public List<DataPoint> getDataPoints() {
    if (this.data == null) {
      this.data = new ArrayList<>();
      try {
        var header = getNameMapping();
        var processors = getProcessors();
        Map<String, Object> datum;
        while ((datum = this.csvReader.read(header, processors)) != null) {
          this.data.add(new DataPoint(this.structure, datum));
        }
      } catch (IOException e) {
        // TODO: Improve.
        throw new RuntimeException(e);
      }
    }
    return data;
  }

  @Override
  public DataStructure getDataStructure() {
    return this.structure;
  }
}
