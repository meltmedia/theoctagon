package com.meltmedia.data;

import java.util.List;
import com.praxissoftware.rest.core.Link;

public class PageLink {
	int pageNumber;
	Link pageLink = new Link();

	public PageLink(int pageNumber, Link pageLink) {
		super();
		this.pageNumber = pageNumber;
		this.pageLink = pageLink;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Link getPageLink() {
		return pageLink;
	}

	public void setPageLink(Link pageLink) {
		this.pageLink = pageLink;
	}

}
