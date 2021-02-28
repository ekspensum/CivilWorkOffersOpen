package pl.aticode.civilworkoffers.dao;

import java.util.List;

import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;

public interface MainPagesRepository {
	
	void saveRecord(MainPages mainPages);
	void updateRecord(MainPages mainPages);
	MainPages findRecord(PageType pageType);
	List<MainPages> findRecords();
}
