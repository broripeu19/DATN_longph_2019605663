package com.devpro.shop16.services;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.transaction.Transactional;

import com.devpro.shop16.entities.BaseEntity;

public abstract class BaseService<E extends BaseEntity>{

private static int SIZE_OF_PAGE = 4;
	
	@PersistenceContext //Inject entityManager
	protected EntityManager entityManager;

	protected abstract Class<E> clazz();

	
	@Transactional
	public E saveOrUpdate(E entity) {
		if (entity.getId() == null || entity.getId() <= 0) {
			entity.setCreatedDate(new Date());
			entityManager.persist(entity); // 
			return entity;
		} else {
			return entityManager.merge(entity); // 
		}
	}

	/**
	 * xÃ³a báº£n ghi trong cÆ¡ sá»Ÿ dá»¯ liá»‡u
	 * @param entity
	 */
	@Transactional
	public void delete(E entity) {
		entityManager.remove(entity);
	}

	/**
	 * xÃ³a báº£n ghi trong cÆ¡ sá»Ÿ dá»¯ liá»‡u theo khÃ³a chÃ­nh id
	 * @param id
	 */
	public void deleteById(int primaryKey) {
		E entity = this.getById(primaryKey);
		delete(entity);
	}
	
	/**
	 * Láº¥y báº£n ghi trong cÆ¡ sá»Ÿ dá»¯ liá»‡u theo khÃ³a chÃ­nh ID.
	 * @param-id
	 * @return
	 */
	public E getById(int primaryKey) {
		return entityManager.find(clazz(), primaryKey);
	}

	/**
	 * thá»±c thi cÃ¢u lá»‡nh truy váº¥n cÆ¡ sá»Ÿ dá»¯ liá»‡u vÃ  tráº£ vá»� duy nháº¥t 1 káº¿t quáº£
	 * @param sql -> vÃ­ dá»¥ cháº¡y cÃ¢u lá»‡nh [SELECT * FROM tbl_category WHERE name='Java']
	 * @param page
	 * @return
	 */
	public E getOneByNativeSQL(String sql) {
		try {
			return executeByNativeSQL(sql, 0).getData().get(0);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Láº¥y táº¥t cáº£ báº£n ghi trong cÆ¡ sá»Ÿ dá»¯ liá»‡u.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<E> findAll() {
		Table tbl = clazz().getAnnotation(Table.class);
		return (List<E>) entityManager.createNativeQuery("SELECT * FROM " + tbl.name(), clazz()).getResultList();
	}
	
	/**
	 * thá»±c thi cÃ¢u lá»‡nh truy váº¥n cÆ¡ sá»Ÿ dá»¯ liá»‡u
	 * @param sql -> vÃ­ dá»¥ cháº¡y cÃ¢u lá»‡nh [SELECT * FROM tbl_category;]
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PagerData<E> executeByNativeSQL(String sql, int page) {
		PagerData<E> result = new PagerData<E>();
		
		try {
			Query query = entityManager.createNativeQuery(sql, clazz());
			
			//trÆ°á»�ng há»£p cÃ³ thá»±c hiá»‡n phÃ¢n trang thÃ¬ káº¿t quáº£ tráº£ vá»�
			//bao gá»“m tá»•ng sá»‘ page vÃ  dá»¯ liá»‡u page hiá»‡n táº¡i
			if(page > 0) {
				result.setCurrentPage(page);
				result.setTotalItems(query.getResultList().size());
				
				query.setFirstResult((page - 1) * SIZE_OF_PAGE);
				query.setMaxResults(SIZE_OF_PAGE);
			}
			
			result.setData(query.getResultList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	public PagerData<E> executeByNativeSQLB(String sql, int page) {
		PagerData<E> result = new PagerData<E>();

		try {
			Query query = entityManager.createNativeQuery(sql, clazz());

			// Trường hợp có thực hiện phân trang thì kết quả trả về
			// bao gồm tổng số page và dữ liệu của page hiện tại
			if (page > 0) {
				int totalItems = query.getResultList().size();
				int firstResult = (page - 1) * SIZE_OF_PAGE;
				int maxResults = SIZE_OF_PAGE;

				// Thiết lập phân trang
				query.setFirstResult(firstResult);
				query.setMaxResults(maxResults);

				// Lấy dữ liệu của page hiện tại
				List<E> data = query.getResultList();

				// Thiết lập kết quả
				result.setCurrentPage(page);
				result.setTotalItems(totalItems);
				result.setData(data);
			} else {
				// Trường hợp không phân trang, lấy toàn bộ dữ liệu
				result.setData(query.getResultList());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}



	/**
	 * thá»±c thi cÃ¢u lá»‡nh cáº­p nháº­t cÆ¡ sá»Ÿ dá»¯ liá»‡u
	 * @param sql -> vÃ­ dá»¥ cháº¡y cÃ¢u lá»‡nh [DELETE FROM tbl_category;] hoáº·c [UPDATE tbl_category SET Name = 'Alfred Schmidt' WHERE Id = 1;]
	 * @return
	 */
	public int executeUpdateOrDeleteByNativeSQL(String sql) {
		try {
			return entityManager.createNativeQuery(sql).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
}
