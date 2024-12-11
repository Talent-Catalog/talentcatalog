/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.util.qr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * QR code image, encoded as Base64 string.
 * <p/>
 * Can be displayed as described here:
 * https://www.w3docs.com/snippets/html/how-to-display-base64-images-in-html.html
 *
 * @author John Cameron
 */
@Getter
@Setter
@AllArgsConstructor
public class EncodedQrImage {
  private String base64Encoding;
}
