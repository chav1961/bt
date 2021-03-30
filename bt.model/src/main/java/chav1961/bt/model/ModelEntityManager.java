package chav1961.bt.model;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.bt.model.interfaces.ORMModelMapper;
import chav1961.bt.model.interfaces.UIModelMapper;
import chav1961.purelib.basic.ReusableInstances;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper.Locker;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This class describes model entity manager to use in the stateless services.</p> 
 */
public class ModelEntityManager implements Closeable {
	private final Map<UUID, ModelEntityRecord<?>>		recordSet = new HashMap<>();
	private final Map<Class<?>, ModelEntityRecord<?>>	classSet = new HashMap<>();
	private final LightWeightRWLockerWrapper			classLock = new LightWeightRWLockerWrapper();

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * <p>Register model entity in the manager. Subsequent call with the same argument returns the same result</p>
	 * @param <T> model entity class
	 * @param metadata descriptor of the model entity. Can't be null 
	 * @param instanceManager manager to create/remove shared instances of the model entities.Can't be null. Will be closed automatically after unregistering model entity
	 * @return UUID for registered entity. Can't be null
	 * @throws IllegalArgumentException on any argument errors
	 * @see #unregisterModelEntity(UUID)
	 */
	public <T> UUID registerModelEntity(final ContentNodeMetadata metadata, final ReusableInstances<T> instanceManager) throws IllegalArgumentException {
		if (metadata == null) {
			throw new IllegalArgumentException("Metadata can't be null");
		}
		else if (instanceManager == null) {
			throw new IllegalArgumentException("Instance manager can't be null");
		}
		else {
			try(final Locker l = classLock.lock(false)) {
				ModelEntityRecord<?>	rec = classSet.get(metadata.getType());
				
				if (rec == null) {
					rec = new ModelEntityRecord(UUID.randomUUID(), metadata.getType(), metadata, instanceManager);
					classSet.put(metadata.getType(), rec);
					recordSet.put(rec.entityId, rec);
				}
				rec.count.incrementAndGet();
				return rec.entityId;
			}
		}
	}

	/**
	 * <p>Unregister model entity in the manager.</p>
	 * @param modelEntityId model entity to unregister. Can't be null. Every call {@linkplain #registerModelEntity(ContentNodeMetadata, ReusableInstances)} method 
	 * should have paired call to this method.
	 * @throws IllegalArgumentException on any argument errors
	 * @see #registerModelEntity(ContentNodeMetadata, ReusableInstances)
	 */
	public void unregisterModelEntity(final UUID modelEntityId) throws IllegalArgumentException {
		if (modelEntityId == null) {
			throw new IllegalArgumentException("Model entity id can't be null");
		}
		else {
			try(final Locker l = classLock.lock(false)) {
				final ModelEntityRecord<?>	rec = recordSet.get(modelEntityId); 
						
				if (rec == null) {
					throw new IllegalArgumentException("Model entity id ["+modelEntityId+"] is not registered in the manager");
				}
				else if (rec.count.decrementAndGet() <= 0) {
					recordSet.remove(rec.entityId);
					classSet.remove(rec.entityClass);
					rec.manager.close();
				}
			}			
		}
	}

	/**
	 * <p>Get ORM mapper for the given model entity</p>
	 * @param <Key> Key type in the database
	 * @param <Data> record type in the database
	 * @param modelEntityId model entity to get ORM mapper for. Can't be null
	 * @return ORM mapper. Can't be null
	 * @throws IllegalArgumentException on any argument errors
	 */
	public <Key,Data> ORMModelMapper<Key, Data> getORMMapperFor(final UUID modelEntityId) throws IllegalArgumentException {
		if (modelEntityId == null) {
			throw new IllegalArgumentException("Model entity id can't be null");
		}
		else {
			try(final Locker l = classLock.lock(true)) {
				final ModelEntityRecord<?>	rec = recordSet.get(modelEntityId);
				
				if (rec == null) {
					throw new IllegalArgumentException("Model entity id ["+modelEntityId+"] is not registered in the manager");
				}
				else {
					return null;
				}
			}
		}
	}

	/**
	 * <p>Get UI mapper for the given model entity</p>
	 * @param modelEntityId model entity to get UI mapper for. Can't be null
	 * @return UI mapper. Can't be null
	 * @throws IllegalArgumentException on any argument errors
	 */
	public UIModelMapper getUIMapperFor(final UUID modelEntityId) throws IllegalArgumentException {
		if (modelEntityId == null) {
			throw new IllegalArgumentException("Model entity id can't be null");
		}
		else {
			try(final Locker l = classLock.lock(true)) {
				final ModelEntityRecord<?>	rec = recordSet.get(modelEntityId);
				
				if (rec == null) {
					throw new IllegalArgumentException("Model entity id ["+modelEntityId+"] is not registered in the manager");
				}
				else {
					return null;
				}
			}
		}
	}

	/**
	 * <p>Get reusable instance of the model entity to use in the program. Every call to this method should have paired call to {@linkplain #freeReusableEntity(Object)} method</p>
	 * @param <T> model entity type
	 * @param modelEntityId model entity to get instance. Can't be null
	 * @return instance returned. Can't be null
	 * @throws IllegalArgumentException on any argument errors
	 * @see #freeReusableEntity(Object)
	 */
	public <T> T getReusableEntity(final UUID modelEntityId) throws IllegalArgumentException {
		if (modelEntityId == null) {
			throw new IllegalArgumentException("Model entity id can't be null");
		}
		else {
			try(final Locker l = classLock.lock(true)) {
				final ModelEntityRecord<?>	rec = recordSet.get(modelEntityId);
				
				if (rec == null) {
					throw new IllegalArgumentException("Model entity id ["+modelEntityId+"] is not registered in the manager");
				}
				else {
					return (T)rec.manager.allocate();
				}
			}
		}
	}
	
	/**
	 * <p>Free reusable instance has been gotten earlier.<p>
	 * @param <T> model entity type
	 * @param entity entity to free. Can't be null
	 * @throws IllegalArgumentException on any argument errors
	 * @see #getReusableEntity(UUID)
	 */
	public <T> void freeReusableEntity(final T entity) throws IllegalArgumentException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity can't be null");
		}
		else {
			try(final Locker l = classLock.lock(true)) {
				final ModelEntityRecord<?>	rec = classSet.get(entity.getClass());
				
				if (rec == null) {
					throw new IllegalArgumentException("Model entity with the class["+entity.getClass()+"] is not registered in the manager");
				}
				else {
					((ReusableInstances<Object>)rec.manager).free(entity);
				}
			}
		}
	}

	private static class ModelEntityRecord<T> {
		private final UUID					entityId;
		private final Class<T>				entityClass;
		private final ContentNodeMetadata	metadata;
		private final ReusableInstances<T>	manager;
		private final AtomicInteger			count = new AtomicInteger();
		
		public ModelEntityRecord(UUID entityId, Class<T> entityClass, ContentNodeMetadata metadata, ReusableInstances<T> manager) {
			this.entityId = entityId;
			this.entityClass = entityClass;
			this.metadata = metadata;
			this.manager = manager;
		}
	}

}
