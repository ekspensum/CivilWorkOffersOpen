package pl.aticode.civilworkoffers.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class LogoSloganFooterRepositoryImpl implements LogoSloganFooterRepository {
	
	@Autowired
	private SessionFactory sessionFactory;

	protected Session getSession() {
		Session session = sessionFactory.getCurrentSession();
		return session;
	}

	@Override
	public void saveRecord(LogoSloganFooter logoSloganFooter) {
		getSession().save(logoSloganFooter);
	}

	@Override
	public void updateRecord(LogoSloganFooter logoSloganFooter) {
		getSession().saveOrUpdate(logoSloganFooter);
	}

	@Override
	public LogoSloganFooter findRecord(long id) {
		return getSession().find(LogoSloganFooter.class, id);
	}

	@Override
	public List<LogoSloganFooter> findRecords() {
		return getSession().createQuery("from LogoSloganFooter", LogoSloganFooter.class).getResultList();
	}

}
