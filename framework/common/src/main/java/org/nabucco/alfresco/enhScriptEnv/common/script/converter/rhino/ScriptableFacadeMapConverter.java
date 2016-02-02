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
package org.nabucco.alfresco.enhScriptEnv.common.script.converter.rhino;

import java.util.Arrays;
import java.util.Map;

import org.alfresco.util.PropertyCheck;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.nabucco.alfresco.enhScriptEnv.common.script.aop.AdapterObject;
import org.nabucco.alfresco.enhScriptEnv.common.script.aop.AdapterObjectInterceptor;
import org.nabucco.alfresco.enhScriptEnv.common.script.aop.MapLengthFacadeInterceptor;
import org.nabucco.alfresco.enhScriptEnv.common.script.aop.ScriptableBaseAdapterInterceptor;
import org.nabucco.alfresco.enhScriptEnv.common.script.aop.ScriptableListLikeMapAdapterInterceptor;
import org.nabucco.alfresco.enhScriptEnv.common.script.aop.ScriptableMapListAdapterInterceptor;
import org.nabucco.alfresco.enhScriptEnv.common.script.aop.ValueConvertingMapInterceptor;
import org.nabucco.alfresco.enhScriptEnv.common.script.converter.ValueConverter;
import org.nabucco.alfresco.enhScriptEnv.common.script.converter.ValueInstanceConverterRegistry;
import org.nabucco.alfresco.enhScriptEnv.common.script.converter.ValueInstanceConverterRegistry.ValueInstanceConverter;
import org.nabucco.alfresco.enhScriptEnv.common.util.ClassUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.NativeMap;

/**
 * A converter to handle conversion for {@link Map maps} that should be exposed via the {@link Scriptable} interface much like the
 * {@link NativeMap Surf native map} wrapper does.
 *
 * @author Axel Faust
 */
public class ScriptableFacadeMapConverter implements ValueInstanceConverter, InitializingBean
{

    protected ValueInstanceConverterRegistry registry;

    /**
     * @param registry
     *            the registry to set
     */
    public void setRegistry(final ValueInstanceConverterRegistry registry)
    {
        this.registry = registry;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "registry", this.registry);

        this.registry.registerValueInstanceConverter(Map.class, this);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int getForJavaConversionConfidence(final Class<?> valueInstanceClass, final Class<?> expectedClass)
    {
        // we won't convert to Java - relying entirely on AdapterObjectConverter
        final int confidence = LOWEST_CONFIDENCE;
        ;
        return confidence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canConvertValueForJava(final Object value, final ValueConverter globalDelegate, final Class<?> expectedClass)
    {
        // we won't convert to Java - relying entirely on AdapterObjectConverter
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertValueForJava(final Object value, final ValueConverter globalDelegate, final Class<?> expectedClass)
    {
        // clients should check canConvertValueForJava first
        throw new UnsupportedOperationException("This operation is not supported and should not have been called");
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int getForScriptConversionConfidence(final Class<?> valueInstanceClass, final Class<?> expectedClass)
    {
        final int confidence;
        if (Map.class.isAssignableFrom(valueInstanceClass) && expectedClass.isAssignableFrom(Scriptable.class))
        {
            confidence = LOW_CONFIDENCE;
        }
        else
        {
            confidence = LOWEST_CONFIDENCE;
        }
        return confidence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canConvertValueForScript(final Object value, final ValueConverter globalDelegate, final Class<?> expectedClass)
    {
        final boolean canConvert = value instanceof Map<?, ?> && expectedClass.isAssignableFrom(Scriptable.class);
        return canConvert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertValueForScript(final Object value, final ValueConverter globalDelegate, final Class<?> expectedClass)
    {
        if (!(value instanceof Map<?, ?>))
        {
            throw new IllegalArgumentException("value must be a Map");
        }

        final Object result;

        final ProxyFactory proxyFactory = new ProxyFactory();

        proxyFactory.addAdvice(new AdapterObjectInterceptor());
        proxyFactory.addAdvice(new ScriptableBaseAdapterInterceptor());
        proxyFactory.addAdvice(new ScriptableListLikeMapAdapterInterceptor());
        proxyFactory.addAdvice(new ScriptableMapListAdapterInterceptor());
        proxyFactory.addAdvice(new MapLengthFacadeInterceptor(Undefined.instance, false));
        // proxyFactory.addAdvice(new ListLikeMapAdapterInterceptor());
        // some existing scripts in Alfresco expect Map-contained strings not to be converted
        proxyFactory.addAdvice(new ValueConvertingMapInterceptor(globalDelegate, true));

        // this somehow worked in Java 8 Nashorn PoC, but return types of remove(Object) differ between Map and List
        // proxyFactory.setInterfaces(ClassUtils.collectInterfaces(value, Arrays.<Class<?>> asList(Scriptable.class, List.class, AdapterObject.class)));
        proxyFactory.setInterfaces(ClassUtils.collectInterfaces(value, Arrays.<Class<?>> asList(Scriptable.class, AdapterObject.class)));

        proxyFactory.setTarget(value);

        result = proxyFactory.getProxy();
        return result;
    }
}
