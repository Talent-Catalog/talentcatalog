
update task set task_type = 'UploadTask',
                upload_subfolder_name = 'Immigration',
                upload_type = 'visa',
                description = 'Please upload a photo of the visa to let us know you have received it. If you have not yet received your visa, do not worry it will come. When it does arrive please return to task and upload a photo of the visa.'
where name = 'visaReturned'
