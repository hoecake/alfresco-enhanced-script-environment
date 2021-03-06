/*
 * Copyright 2016 Axel Faust
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package de.axelfaust.alfresco.enhScriptEnv.common.script.aop;

/**
 *
 * @author Axel Faust
 */
public enum NativeArrayFunctionName
{
    PUSH, POP, SHIFT, UNSHIFT, SPLICE, UNKNOWN;

    protected static NativeArrayFunctionName functionLiteralOf(final String methodName)
    {
        NativeArrayFunctionName value = UNKNOWN;

        for (final NativeArrayFunctionName literal : values())
        {
            if (literal.name().equalsIgnoreCase(methodName))
            {
                value = literal;
                break;
            }
        }

        return value;
    }
}