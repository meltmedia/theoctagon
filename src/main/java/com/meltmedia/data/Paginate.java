package com.meltmedia.data;

import java.util.ArrayList;
import java.util.List;

public class Paginate {
	int currentPage;
	int nextPage;
	int previousPage;
	List<PageLink> pageLinks = new ArrayList<PageLink>();

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getNextPage() {
		return nextPage;
	}

	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}

	public int getPreviousPage() {
		return previousPage;
	}

	public void setPreviousPage(int previousPage) {
		this.previousPage = previousPage;
	}

	public List<PageLink> getPageLinks() {
		return pageLinks;
	}

	public void setPageLinks(List<PageLink> pageLinks) {
		this.pageLinks = pageLinks;
	}

}
