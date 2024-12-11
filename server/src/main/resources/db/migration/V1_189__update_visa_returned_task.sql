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

update task set task_type = 'UploadTask',
                upload_subfolder_name = 'Immigration',
                upload_type = 'visa',
                description = 'Please upload a photo of the visa to let us know you have received it. If you have not yet received your visa, do not worry it will come. When it does arrive please return to task and upload a photo of the visa.'
where name = 'visaReturned'
