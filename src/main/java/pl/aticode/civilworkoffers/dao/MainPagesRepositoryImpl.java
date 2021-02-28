package pl.aticode.civilworkoffers.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class MainPagesRepositoryImpl implements MainPagesRepository {
	
	@Autowired
	private SessionFactory sessionFactory;

	protected Session getSession() {
		Session session = sessionFactory.getCurrentSession();
		return session;
	}

	@Override
	public void saveRecord(MainPages mainPages) {
		getSession().save(mainPages);
	}

	@Override
	public void updateRecord(MainPages mainPages) {
		getSession().saveOrUpdate(mainPages);
	}

	@Override
	public MainPages findRecord(PageType pageType) {
		return getSession().createNamedQuery("findMainPageWithPrameter", MainPages.class).setParameter("pageType", pageType).getSingleResult();
	}

	@Override
	public List<MainPages> findRecords() {
		return getSession().createQuery("from MainPages", MainPages.class).getResultList();
	}


}
