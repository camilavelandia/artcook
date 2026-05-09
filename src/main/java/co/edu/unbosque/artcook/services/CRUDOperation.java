package co.edu.unbosque.artcook.services;

import java.util.List;

public interface CRUDOperation<T> {
	
	public int create(T newData);
	
	public List<T> getAll();
	
	public int deleteById(Long id);
	
	public int updateById(Long id, T newData);
	
	public long count();
	
	public boolean exist(Long id);
}