
insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('predepartureVideo','Provide link to your pre-departure video/s', 'QuestionTask',
        'The IOM have asked if candidates can do some pre-departure filming (on your mobile phones) that could then be collected and put in a film they are putting together about your arrivals. If you have some videos you would like to put forward, please upload them to a shareable drive (Dropbox, Google Drive etc) and provide the shareable link below.',
        7, (select id from users where username = 'SystemAdmin'), now());
