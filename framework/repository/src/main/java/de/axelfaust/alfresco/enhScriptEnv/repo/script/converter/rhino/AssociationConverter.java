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
package de.axelfaust.alfresco.enhScriptEnv.repo.script.converter.rhino;

import org.alfresco.repo.jscript.Association;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.util.PropertyCheck;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import de.axelfaust.alfresco.enhScriptEnv.common.script.converter.ValueConverter;
import de.axelfaust.alfresco.enhScriptEnv.common.script.converter.ValueInstanceConverterRegistry;
import de.axelfaust.alfresco.enhScriptEnv.common.script.converter.ValueInstanceConverterRegistry.ValueInstanceConverter;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Axel Faust
 */
public class AssociationConverter implements ValueInstanceConverter, InitializingBean
{

    private static final Scriptable DUMMY_SCOPE;
    static
    {
        final Context cx = Context.enter();
        try
        {
            DUMMY_SCOPE = cx.initStandardObjects(null, true);
            DUMMY_SCOPE.delete("Packages");
            DUMMY_SCOPE.delete("getClass");
            DUMMY_SCOPE.delete("java");
            ((ScriptableObject) DUMMY_SCOPE).sealObject();
        }
        finally
        {
            Context.exit();
        }
    }

    protected ValueInstanceConverterRegistry registry;

    protected ServiceRegistry serviceRegistry;

    /**
     * @param registry
     *            the registry to set
     */
    public void setRegistry(final ValueInstanceConverterRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * @param serviceRegistry
     *            the serviceRegistry to set
     */
    public void setServiceRegistry(final ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "registry", this.registry);
        PropertyCheck.mandatory(this, "serviceRegistry", this.serviceRegistry);

        this.registry.registerValueInstanceConverter(AssociationRef.class, this);
        this.registry.registerValueInstanceConverter(Association.class, this);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int getForJavaConversionConfidence(final Class<?> valueInstanceClass, final Class<?> expectedClass)
    {
        final int confidence;
        if (Association.class.isAssignableFrom(valueInstanceClass) && expectedClass.isAssignableFrom(AssociationRef.class))
        {
            confidence = HIGHEST_CONFIDENCE;
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
    public boolean canConvertValueForJava(final Object value, final ValueConverter globalDelegate, final Class<?> expectedClass)
    {
        final boolean canConvert = value instanceof Association && expectedClass.isAssignableFrom(AssociationRef.class);
        return canConvert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertValueForJava(final Object value, final ValueConverter globalDelegate, final Class<?> expectedClass)
    {
        if (!(value instanceof Association))
        {
            throw new IllegalArgumentException("value must be a Association");
        }

        final Association association = (Association) value;
        final AssociationRef associationRef = association.getAssociationRef();

        return associationRef;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int getForScriptConversionConfidence(final Class<?> valueInstanceClass, final Class<?> expectedClass)
    {
        final int confidence;
        if (AssociationRef.class.isAssignableFrom(valueInstanceClass) && expectedClass.isAssignableFrom(Association.class))
        {
            confidence = HIGHEST_CONFIDENCE;
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
        final boolean canConvert = value instanceof AssociationRef && expectedClass.isAssignableFrom(Association.class);
        return canConvert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertValueForScript(final Object value, final ValueConverter globalDelegate, final Class<?> expectedClass)
    {
        if (!(value instanceof AssociationRef))
        {
            throw new IllegalArgumentException("value must be a AssociationRef");
        }

        final AssociationRef associationRef = (AssociationRef) value;

        final Association result = new Association(this.serviceRegistry, associationRef, DUMMY_SCOPE);

        return result;
    }
}