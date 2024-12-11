
insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('maritalStatus','What is your marital status?', 'QuestionTask', 'Please select which option best describes your status:',
        7, 'maritalStatus',
        (select id from users where username = 'SystemAdmin'), now());


insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('childrenToUk','How many children do you want to bring to the UK?', 'QuestionTask', 'Please write how many of each gender: (if no children put 0)',
        7,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('covidVaccinatedStatus','Are you fully vaccinated (double dose) or partially vaccinated (1st dose only) against Covid-19?', 'QuestionTask', 'Please select: (If not vaccinated, please check abandon task and provide details)',
        7, 'covidVaccinatedStatus',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('covidVaccinatedDate','What was the date of your last Covid-19 vaccination?', 'QuestionTask', 'Please select: (If not vaccinated, please check abandon task and provide details)',
        7, 'covidVaccinatedDate',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('covidVaccinatedName','What was the name of your last Covid-19 vaccination?', 'QuestionTask', 'Please select: (If not vaccinated, please check abandon task and provide details)',
        7, 'covidVaccineName',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('familyFriendsUkLoc','If you have family/friends in the UK where are they located?', 'QuestionTask', 'Please provide relationship (including if they are a TBB candidate) and location: (if no family/friends in UK abandon task and add comment)',
        7,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('placedTbbCandidate','Do you want to be placed with another TBB candidate?', 'QuestionTask', 'Please select: (If yes, please add comment who and where they are in the UK)',
        7, 'Yes,No',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('noticePeriod','How long is your notice period?', 'QuestionTask', 'Please put number of days/weeks/months: (if no notice period put 0)',
        7,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('mediaWillingness','Do you agree to be included in media (videos, articles and other media) that is shared by TBB and partners on social media?', 'QuestionTask', 'TBB often takes videos of airport arrivals, graduation ceremonies, interviews candidates for blog and news articles, etc. We share this media on social media pages including Linkedin, Twitter, and other platforms. Please select: (If maybe, please add comment what you are/are not ok with)',
        7, 'mediaWillingness',
        (select id from users where username = 'SystemAdmin'), now());
