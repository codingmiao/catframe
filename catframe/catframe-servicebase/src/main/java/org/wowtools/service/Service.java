package org.wowtools.service;


import org.wowtools.common.utils.CopyPropertiesUtil;
import org.wowtools.dto.DTO;
import org.wowtools.entity.Entity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Service<D extends DTO<T>, E extends Entity<T>, T> {
    protected final CopyPropertiesUtil<D, E> copyPropertiesUtil;
    protected final Class<? extends D> classDto;
    protected final Class<? extends E> classEntity;

    public Service() {
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = pt.getActualTypeArguments();
        classDto = (Class<D>) types[0];
        classEntity = (Class<E>) types[1];
        copyPropertiesUtil = new CopyPropertiesUtil<>(classDto, classEntity);
    }

    public abstract void save(D d);

    public abstract void update(D d);

    public void delete(D d) {
        delete(d.getId());
    }

    public abstract void delete(T id);

    public E toEntity(D d) {
        try {
            E e = classEntity.newInstance();
            copyPropertiesUtil.copyA2b(d, e);
            return e;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    public D toDTO(E e) {
        try {
            D d = classDto.newInstance();
            copyPropertiesUtil.copyB2a(d, e);
            return d;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

}
