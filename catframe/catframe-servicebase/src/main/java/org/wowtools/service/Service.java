package org.wowtools.service;

import javax.persistence.Entity;

import org.wowtools.dto.DTO;

public interface Service<D extends DTO,E extends Entity> {

	public void save(D d);
	
	public void update(D d);
	
	/**
	 * 删除，若
	 * @param d
	 */
	public void delete(D d);
	
}
