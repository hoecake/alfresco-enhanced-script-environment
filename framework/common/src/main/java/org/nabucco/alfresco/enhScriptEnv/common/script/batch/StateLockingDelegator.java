/*
 * Copyright 2013 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.nabucco.alfresco.enhScriptEnv.common.script.batch;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Instances of this class are {@link Scriptable} and {@link Function} delegators / interceptors that guarantee
 * atomicity for script object manipulation and function invocation in a multi-threaded environment. In the case of
 * functions, instances will also take care to write-lock the state of their owning objects so that internal state
 * change due to execution of the function will appear atomic to other threads.
 *
 * @author Axel Faust, <a href="http://www.prodyna.com">PRODYNA AG</a>
 */
public class StateLockingDelegator extends ObjectFacadingDelegator
{
	protected static final Map<Scriptable, Map<Scriptable, StateLockingDelegator>> DELEGATOR_MAP_BY_SCOPE_CONTEXT = new WeakHashMap<Scriptable, Map<Scriptable, StateLockingDelegator>>();

	protected final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock(true);

	protected final ReentrantReadWriteLock ownerStateLock;

	public StateLockingDelegator(final Scriptable refereneScope, final Scriptable delegee,
			final ObjectFacadeFactory facadeFactory)
	{
		super(refereneScope, delegee, facadeFactory);
		this.ownerStateLock = null;
	}

	public StateLockingDelegator(final Scriptable referenceScope, final Scriptable delegee,
			final ObjectFacadeFactory facadeFactory, final StateLockingDelegator owner)
	{
		super(referenceScope, delegee, facadeFactory);
		this.ownerStateLock = owner.stateLock;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(final String name, final Scriptable start)
	{
		// since get operations need to iterate over slots, we need to read-lock to protected against concurrent
		// modification
		this.stateLock.readLock().lock();
		try
		{
			return super.get(name, start);
		} finally
		{
			this.stateLock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(final int index, final Scriptable start)
	{
		// since get operations need to iterate over slots, we need to read-lock to protected against concurrent
		// modification
		this.stateLock.readLock().lock();
		try
		{
			return super.get(index, start);
		} finally
		{
			this.stateLock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean has(final String name, final Scriptable start)
	{
		// since has operations need to iterate over slots, we need to read-lock to protected against concurrent
		// modification
		this.stateLock.readLock().lock();
		try
		{
			return super.has(name, start);
		} finally
		{
			this.stateLock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean has(final int index, final Scriptable start)
	{
		// since has operations need to iterate over slots, we need to read-lock to protected against concurrent
		// modification
		this.stateLock.readLock().lock();
		try
		{
			return super.has(index, start);
		} finally
		{
			this.stateLock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(final String name, final Scriptable start, final Object value)
	{
		this.stateLock.writeLock().lock();
		try
		{
			super.put(name, start, value);
		} finally
		{
			this.stateLock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(final int index, final Scriptable start, final Object value)
	{
		this.stateLock.writeLock().lock();
		try
		{
			super.put(index, start, value);
		} finally
		{
			this.stateLock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(final String name)
	{
		this.stateLock.writeLock().lock();
		try
		{
			super.delete(name);
		} finally
		{
			this.stateLock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(final int index)
	{
		this.stateLock.writeLock().lock();
		try
		{
			super.delete(index);
		} finally
		{
			this.stateLock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Scriptable getPrototype()
	{
		// getPrototype is atomic by nature, but our superclass executes non-atomic facading logic
		this.stateLock.readLock().lock();
		try
		{
			return super.getPrototype();
		} finally
		{
			this.stateLock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Scriptable getParentScope()
	{
		// getParentScope is atomic by nature, but our superclass executes non-atomic facading logic
		this.stateLock.readLock().lock();
		try
		{
			return super.getParentScope();
		} finally
		{
			this.stateLock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getIds()
	{
		this.stateLock.readLock().lock();
		try
		{
			return super.getIds();
		} finally
		{
			this.stateLock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args)
	{
		// TODO: should we inspect method to determine modification probability, e.g. read-lock on get/find/search
		// functions?

		// assume execution can modify function state
		this.stateLock.writeLock().lock();
		try
		{
			// assume execution can modify owner state
			if (this.ownerStateLock != null)
			{
				this.ownerStateLock.writeLock().lock();
			}
			try
			{
				return super.call(cx, scope, thisObj, args);
			} finally
			{
				if (this.ownerStateLock != null)
				{
					this.ownerStateLock.writeLock().unlock();
				}
			}
		} finally
		{
			this.stateLock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Scriptable construct(final Context cx, final Scriptable scope, final Object[] args)
	{
		// assume execution can modify function state
		this.stateLock.writeLock().lock();
		try
		{
			// assume execution can modify owner state
			if (this.ownerStateLock != null)
			{
				this.ownerStateLock.writeLock().lock();
			}
			try
			{
				return super.construct(cx, scope, args);
			} finally
			{
				if (this.ownerStateLock != null)
				{
					this.ownerStateLock.writeLock().unlock();
				}
			}
		} finally
		{
			this.stateLock.writeLock().unlock();
		}
	}

}