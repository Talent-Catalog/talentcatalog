---
title: Release Notes 2.3.0
description: Duolingo English Test
sass:
  style: compressed
---
## Duolingo English Test Integration
The Talent Catalog is happy to announce a partnership with Duolingo, allowing selected candidates to access free coupons to take the Duolingo English Test (DET). 
The DET is a modern English proficiency assessment available online, taking approximately 1 hour and results being returned in 2 days. 

Working with Duolingo's API we are able to manage the coupons and track the results directly through the Talent Catalog.
Through the Talent Catalog admin portal, admins can:

### Upload coupons provided by Duolingo to the TC
<div class="card-image-container-narrow">
    <img src="./../assets/images/v230/DuolingoAdminUpload.png" 
    alt="Upload coupons via settings tab on admin portal" class="card-image">
</div>

### Assign coupons to a list of candidates or a candidate individually, and set a timeframe for the test to be taken</li>
<div class="card-image-container">
    <div class="card-image">
        <img src="./../assets/images/v230/DuolinoAdminCouponList.png" alt="Assign coupon to list">
        <div class="card-image-caption">Assign coupons to a list</div>
    </div>
    <div class="card-image">
        <img src="./../assets/images/v230/DuolingoAdminCouponCandidate.png" alt="Assign coupon to candidate">
        <div class="card-image-caption">Assign coupon to a candidate</div>
    </div>
</div>

### Monitor if/when the candidate has redeemed the coupon AND completed the test
When coupons are assigned to a candidate, tasks are created to assist with monitoring of a candidate's progress. 
When a coupon is first assigned they are also assigned a 'Claim DET Coupon' task. Once they have claimed this coupon by 
clicking the 'Claim Coupon' button on the candidate portal (see candidate's view below) then that task will be autocompleted, 
and the candidate will be assigned another task - 'Take DET test'. These two tasks can be monitored using our existing 
tasks monitor symbol in lists and searches, and it can be viewed in more detail by viewing the Tasks tab on the candidate profile.
view
<div class="card-image-container">
    <div class="card-image">
        <img src="./../assets/images/v230/DuolingoAdminMonitor.png" alt="Monitor candidate coupons through tasks">
    </div>
</div>

### View the DET test results
Every day the TC will pull the latest DET results from Duolingo and import those results into the appropriate candidate's Language Exams 
section of their candidate profile, under the Intake tabs. These results are detailed in within the exam card but there 
is also a results overview in the accordion header. The results are coloured Green, Yellow, Red depending on which result category they fall in.
<div class="card-image-container">
    <div class="card-image">
        <img src="./../assets/images/v230/DuolingoAdminResults.png" alt="View candidate DET results through their language exams">
    </div>
</div>

Through the Talent Catalog candidate portal, candidate's can:
<ul>
    <li>Learn more about the Duolingo DET test via the services tab</li>
    <li>Redeem Duolingo's free DET coupon</li>
    <li>Connect to the free DET test and complete within the set timeframe</li>
</ul>
