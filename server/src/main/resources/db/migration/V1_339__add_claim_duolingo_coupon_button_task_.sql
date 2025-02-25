insert into task (admin, created_by, created_date, days_to_complete, description, name, optional, task_type, display_name)
values (false, (select id from users where username = 'SystemAdmin'), now(), 14,
        'Track if the user clicked on Claim Coupon button.', 'claimCouponButton', false, 'Task',
        'Claim the Duolingo Coupon');
