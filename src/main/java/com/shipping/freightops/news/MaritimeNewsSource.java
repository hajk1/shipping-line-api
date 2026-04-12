package com.shipping.freightops.news;

import com.shipping.freightops.dto.MaritimeNewsArticle;
import java.util.List;

public interface MaritimeNewsSource {

  List<MaritimeNewsArticle> getRecentHeadlines(String route, int maxResults);
}
