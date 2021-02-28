package pl.aticode.civilworkoffers.dao;

import java.util.List;

import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;

public interface LogoSloganFooterRepository {
	void saveRecord(LogoSloganFooter logoSloganFooter);
	void updateRecord(LogoSloganFooter logoSloganFooter);
	LogoSloganFooter findRecord(long id);
	List<LogoSloganFooter> findRecords();
}
