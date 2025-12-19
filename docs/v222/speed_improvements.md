---
title: Speed Improvements
sass:
  style: compressed
---

With the increasing scale of the Talent Catalog, there have been some speed issues which have impacted the user experience. 
Along with various bug fixes, we have implemented some other actions to improve the speed.


# New way of running stats on saved searches

Temporarily there are two ways of running stats, and two buttons "Run stats" and "New run stats".

They should both produce the same results, but the new way should get over one of the
limitations of the old way - for example, reported by Sarah Walder where she could not run stats
on searches showing candidates in Lebanon or Jordan because there were too many of them
(greater than 32,000). The new way of running stats should avoid that limitation.

Once we are happy that the new way of running stats produces the same results as the old way,
we can remove the old way altogether.

<div class="card-image-container">
  <img src="./../assets/images/v222/NewStatsButton.png" 
        alt="New Run Stats button" class="card-image">
</div>


# Candidate name & number dropdown speed

By changing how we query the database of candidates, this dropdown is able to retrieve results much quicker.

# TC Load Timeout

Occasionally the TC would max out on memory causing the TC to 

