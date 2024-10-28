I chose Spring boot framework because of ease of development of the web services.
I tried to follow BDD-TDD development practices as much as possible (Visible in the git commits).
I tried to cover functionalities with tests - integration tests that are using stubs and unit tests - that using mocks.
For caching I used caffeine since it was easy to setup and use.
From Caching POV I chose the most straight forward solution with @Cacheable annotation provided by spring framework
All the solutions that I have tried were still feed backed with the following error message
"Some customers reported that they got companies with wrong or invalid dates."
To solve this I tried several approaches.
1. I thought this was coming up because of invalid date or invalid format in the closed_on/dissolved_on dates that is passed over to the client. So I have added validations for these.
2. I thought this could be because of stale data where the data has changed after being cached. I used cache control headers to identify and fetch the recent most data.
3. I thought this could be because of stale data where the company is due to be closed within 24 hrs (before the cache invalidated). I calculated expiry programmatically based on the closed on date.
4. You can find these implementations in the git history. I removed them considering this is not within the scope of this exercise and left with simple implementation of @cacheable annotation

I have also added some basic metrics to get visibility in to the client calls, backend calls and errors.

Obviously there are more improvements that can be made in the current implementation such as logging, refactoring, adding more testing etc.