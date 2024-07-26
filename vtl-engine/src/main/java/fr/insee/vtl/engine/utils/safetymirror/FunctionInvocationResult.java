package fr.insee.vtl.engine.utils.safetymirror;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * Copyright 2016 Anders Granau Høfft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * END OF NOTICE
 *
 * @author Anders Granau Høfft
 */
@Builder
@Data
public class FunctionInvocationResult<RESULT> {

    Method method;
    Object[] arguments;
    RESULT result;
    Exception exception;

    public boolean exceptionThrown(){
        return exception!=null;
    }

}
