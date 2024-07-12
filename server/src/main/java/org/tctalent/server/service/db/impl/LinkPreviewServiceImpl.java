/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static java.lang.Double.parseDouble;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.repository.db.LinkPreviewRepository;
import org.tctalent.server.service.db.LinkPreviewService;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkPreviewServiceImpl implements LinkPreviewService {

  private final LinkPreviewRepository linkPreviewRepository;

  @Override
  public boolean deleteLinkPreview(long linkPreviewId)
      throws EntityReferencedException, InvalidRequestException {
    linkPreviewRepository.deleteById(linkPreviewId);
    return true;
  }

  @Override
  public LinkPreview buildLinkPreview(String url) throws IOException {
    LinkPreview linkPreview = new LinkPreview();
    linkPreview.setUrl(url);

    try {
      Document doc = Jsoup.connect(url).get();

      linkPreview.setDescription(getDescription(doc));
      linkPreview.setTitle(getTitle(doc));
      linkPreview.setDomain(getDomain(doc, url));
      linkPreview.setImageUrl(getImageUrl(doc));
      linkPreview.setFaviconUrl(getFaviconUrl(doc));

      return linkPreview;
    } catch (HttpStatusException e) {
      LogBuilder.builder(log)
          .action("BuildLinkPreview: Jsoup.connect(url).get()")
          .message(
              String.format("HTTP error fetching URL " + "(" + url + "): " + e.getStatusCode())
          )
          .logInfo();

      return null;
    }

  }

  private String getTitle(Document document) {

    String title = document.title();
    if (!title.isEmpty()) return title;

    Elements ogTitleElements = document.select("meta[property=\"og:title\"]");
    if (!ogTitleElements.isEmpty()) {
      String ogTitle = ogTitleElements.first().attr("content");
      if (!ogTitle.isEmpty()) return ogTitle;
    }

    Elements twitterTitleElements = document.select("meta[name=\"twitter:title\"]");
    if (!twitterTitleElements.isEmpty()) {
      String twitterTitle = twitterTitleElements.first().attr("content");
      if (!twitterTitle.isEmpty()) return twitterTitle;
    }

    Elements h1Elements = document.select("h1");
    if (!h1Elements.isEmpty()) {
      String h1 = h1Elements.first().text();
      if (!h1.isEmpty()) return h1;
    }

    Elements h2Elements = document.select("h2");
    if (!h2Elements.isEmpty()) {
      String h2 = h2Elements.first().text();
      if (!h2.isEmpty()) return h2;
    }

    return null;
  }

  private String getDescription(Document document) {

    Elements ogDescriptionElements = document.select("meta[property=\"og:description\"]");
    if (!ogDescriptionElements.isEmpty()) {
      String ogDescription = ogDescriptionElements.first().attr("content");
      if (!ogDescription.isEmpty()) return ogDescription;
    }

    Elements twitterDescriptionElements = document.select("meta[name=\"twitter:description\"]");
    if (!twitterDescriptionElements.isEmpty()) {
      String twitterDescription = twitterDescriptionElements.first().attr("content");
      if (!twitterDescription.isEmpty()) return twitterDescription;
    }

    Elements metaDescriptionElements = document.select("meta[name=\"description\"]");
    if (!metaDescriptionElements.isEmpty()) {
      String metaDescription = metaDescriptionElements.first().attr("content");
      if (!metaDescription.isEmpty()) return metaDescription;
    }

    Elements paragraphs = document.select("p");
    if (!paragraphs.isEmpty()) {
      String firstPara = paragraphs.first().text();
      if (!firstPara.isEmpty()) return firstPara;
    }

    return null;
  }

  private String getDomain(Document document, String url) throws MalformedURLException {
    Elements canonicalLinkElements = document.select("link[rel=canonical]");
    if (!canonicalLinkElements.isEmpty()) {
      String canonicalLink = canonicalLinkElements.first().attr("href");
      if (!canonicalLink.isEmpty()) {
        String domain = URI.create(canonicalLink).toURL().getHost().replace("www.", "");
        if (domain != null && !domain.isEmpty()) return domain;
      }
    }

    Elements ogUrlMetaElements = document.select("meta[property=\"og:url\"]");
    if (!ogUrlMetaElements.isEmpty()) {
      String ogUrlMeta = ogUrlMetaElements.first().attr("content");
      if (!ogUrlMeta.isEmpty()) {
        String domain = URI.create(ogUrlMeta).toURL().getHost().replace("www.", "");
        if (domain != null && !domain.isEmpty()) return domain;
      }
    }

    String domain = URI.create(url).toURL().getHost().replace("www.", "");
    if (domain != null && !domain.isEmpty()) return domain;

    return null;
  }

  private String getImageUrl(Document document) {

    Elements ogImgElements = document.select("meta[property=\"og:image\"]");
    if (!ogImgElements.isEmpty()) {
      String ogImg = selectImage("content", ogImgElements);
      if (ogImg != null && !ogImg.isEmpty()) return ogImg;
    }

    Elements imgRelLinkElements = document.select("link[rel=\"image_src\"]");
    if (!imgRelLinkElements.isEmpty()) {
      String imgRelLink = selectImage("href", imgRelLinkElements);
      if (imgRelLink != null && !imgRelLink.isEmpty()) return imgRelLink;
    }

    Elements twitterImgElements = document.select("meta[name=\"twitter:image\"]");
    if (!twitterImgElements.isEmpty()) {
      String twitterImg = selectImage("content", twitterImgElements);
      if (twitterImg != null && !twitterImg.isEmpty()) return twitterImg;
    }

    Elements images = document.getElementsByTag("img");
    if (!images.isEmpty()) {
      Elements qualifiedImages = images
          .stream()
          .filter(img -> img.hasAttr("width") && img.hasAttr("height"))
          .filter(img ->
              (parseDouble(img.attr("width").replace("px","")) /
                  parseDouble(img.attr("height").replace("px",""))) < 3)
          .filter(img ->
              (parseDouble(img.attr("width").replace("px","")) > 50) &&
                  (parseDouble(img.attr("height").replace("px","")) > 50))
          .collect(Collectors.toCollection(Elements::new));

      if (!qualifiedImages.isEmpty()) {
        String qualifiedImage = selectImage("src", qualifiedImages);
        if (qualifiedImage != null && !qualifiedImage.isEmpty()) return qualifiedImage;
      }
    }

    return null;
  }

  private String getFaviconUrl(Document document) {
    Elements faviconLinkElements = document.head().select("link[href~=.*\\.(ico|png)]");
    if (!faviconLinkElements.isEmpty()) {
      String faviconLink = selectImage("href", faviconLinkElements);
      if (faviconLink != null && !faviconLink.isEmpty()) return faviconLink;
    }

    Elements faviconMetaElements = document.head().select("meta[itemprop=image]");
    if (!faviconMetaElements.isEmpty()) {
      String faviconMeta = selectImage("content", faviconMetaElements);
      if (faviconMeta != null && !faviconMeta.isEmpty()) return faviconMeta;
    }

    return null;
  }

  private @Nullable String selectImage(String attr, Elements elements) {
    int i = 0;
    while (i < elements.size()) {
      if (elements.get(i).hasAttr(attr) && elements.get(i).attr(attr).startsWith("http")) {
        String imageUrl = elements.get(i).attr(attr);
        if (checkImageIsAccessible(imageUrl)) return imageUrl;
      }
      i++;
    }

    return null;
  }

  private boolean checkImageIsAccessible(String imageUrl) {
    try {
      Connection.Response response = Jsoup.connect(imageUrl)
          .ignoreContentType(true)
          .execute();

      int statusCode = response.statusCode();
      // Return true if status code is 200 (OK)
      return statusCode == 200;
    } catch (IOException e) {
      LogBuilder.builder(log)
          .action("CheckImageIsAccessible")
          .message(
              String.format("Error fetching image with URL " + imageUrl + ": " + e.getMessage())
          )
          .logInfo();

      // Return false if there was an exception or non-200 status
      return false;
    }
  }
}
