/*
 * Copyright 2015 Axel Faust
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

import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.mozilla.javascript.NativeJavaObject;

/**
 * @author Axel Faust
 */
public class NativeJavaMethodArgumentCorrectingInterceptor implements MethodInterceptor
{

    protected final Map<Object, NativeJavaObject> nativeObjectCache;

    public NativeJavaMethodArgumentCorrectingInterceptor(final Map<Object, NativeJavaObject> nativeObjectCache)
    {
        this.nativeObjectCache = nativeObjectCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        Object result;

        final Object[] arguments = invocation.getArguments();
        for (int idx = 0; idx < arguments.length; idx++)
        {
            if (arguments[idx] instanceof AdapterObject)
            {
                final Object baseObject = ((AdapterObject) arguments[idx]).getBackingObject();
                final NativeJavaObject nativeObject = this.nativeObjectCache.get(baseObject);
                if (nativeObject != null)
                {
                    arguments[idx] = nativeObject;
                }
            }
        }

        result = invocation.proceed();

        return result;
    }

}
