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
package de.axelfaust.alfresco.enhScriptEnv.common.script.batch;

import org.alfresco.util.Pair;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Basic batch execution worker class which delegates callback invocations to the batch function. This mostly serves as a base class for
 * environment specific batch workers.
 *
 * @author Axel Faust
 */
public class BaseExecuteBatchWorker<EBF extends AbstractExecuteBatchFunction>
{
    protected final EBF batchFunction;

    protected final Context parentContext = Context.getCurrentContext();

    protected final Scriptable parentScope;
    protected final Scriptable thisObj;
    protected final Pair<Scriptable, Function> processCallback;
    protected final Pair<Scriptable, Function> beforeProcessCallback;
    protected final Pair<Scriptable, Function> afterProcessCallback;

    protected final ThreadLocal<Scriptable> processScope = new ThreadLocal<Scriptable>();

    public BaseExecuteBatchWorker(final EBF batchFunction, final Scriptable parentScope, final Scriptable thisObj,
            final Pair<Scriptable, Function> processCallback, final Pair<Scriptable, Function> beforeProcessCallback,
            final Pair<Scriptable, Function> afterProcessCallback)
    {
        this.batchFunction = batchFunction;
        this.processCallback = processCallback;
        this.beforeProcessCallback = beforeProcessCallback;
        this.afterProcessCallback = afterProcessCallback;

        this.parentScope = parentScope;
        this.thisObj = thisObj;
    }

    protected void doBeforeProcess()
    {
        final Scriptable processScope = this.batchFunction.doBeforeProcess(this.parentContext, this.parentScope, this.thisObj,
                this.beforeProcessCallback);
        this.processScope.set(processScope);
    }

    protected void doAfterProcess()
    {
        this.batchFunction.doAfterProcess(this.parentContext, this.parentScope, this.processScope.get(), this.thisObj,
                this.afterProcessCallback);
    }

    protected void doProcess(final Object element)
    {
        this.batchFunction.doProcess(this.parentContext, this.parentScope, this.processScope.get(), this.thisObj, this.processCallback,
                element);
    }
}