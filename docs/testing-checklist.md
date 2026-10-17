# Manual runtime testing checklist

Run with JDK 17+ and Apache Derby embedded mode. Use synthetic employee data. The Actual Result column is blank and each Status starts as Pending; update both after running each test in NetBeans. Source compilation alone is not evidence that runtime testing passed.

| ID | Requirement / Function | Test input or action | Expected result | Actual result | Status |
|---|---|---|---|---|---|
| TC-01 | First-run database setup | Launch with no `hrmsdb` folder | Derby database and all six tables are created; demo accounts and starter reference records are seeded | | Pending |
| TC-02 | Administrator login | `admin` / `admin123` | Administrator dashboard and user-account management are available | | Pending |
| TC-03 | HR Staff login | `hrstaff` / `hr123` | Dashboard opens; User Accounts are not available | | Pending |
| TC-04 | Invalid login | Correct username with incorrect password | Access is denied and an understandable message appears | | Pending |
| TC-05 | Login Clear/Reset | Type username/password, click Clear / Reset | Both fields are cleared and focus returns to username | | Pending |
| TC-06 | Login Exit | Click Exit and confirm | Application closes | | Pending |
| TC-07 | Add department/position | Enter valid unique data | Records save and appear in tables/dropdowns | | Pending |
| TC-08 | Duplicate department/position | Reuse existing unique name/title | Database prevents duplicate and UI shows a friendly message | | Pending |
| TC-09 | Employee create/view | Add valid employee with department and position | Employee row appears with joined department/position labels | | Pending |
| TC-10 | Required-field validation | Leave required employee field blank | Save is rejected and the missing field is identified | | Pending |
| TC-11 | Email validation | Enter malformed email | Save is rejected with a validation message | | Pending |
| TC-12 | Employee update | Select employee, change phone/status, save changes | Updated details persist after refresh | | Pending |
| TC-13 | Search and sorting | Search a known employee name/number; click table headers | Matching rows are displayed and rows can be sorted | | Pending |
| TC-14 | Role-based delete | Sign in as HR Staff and inspect Delete controls / handler | Delete is disabled and a direct delete attempt is rejected | | Pending |
| TC-15 | Delete confirmation | Administrator deletes an unreferenced record | Confirmation is displayed before deletion | | Pending |
| TC-16 | Foreign-key integrity | Try deleting department/position/employee referenced by a record | Database prevents invalid relationship; UI displays a clear explanation | | Pending |
| TC-17 | Attendance create/update | Add attendance with valid date and times, then update | Row persists and time values are displayed in 12-hour AM/PM format | | Pending |
| TC-18 | Overnight attendance | Date 2026-10-17, Time In 07:00 PM, Time Out 06:00 AM | Record saves; Time Out displays as 6:00 AM (next day); no validation rejection occurs | | Pending |
| TC-19 | Same-day attendance | Time In 08:00 AM, Time Out 05:00 PM | Record saves and Time Out is not marked next day | | Pending |
| TC-20 | Invalid AM/PM time | Enter 13:75 for a time or leave a typed value malformed | Save is rejected with a clear 12-hour time validation message | | Pending |
| TC-21 | Duplicate attendance | Add second attendance for same employee/date | Unique constraint rejects the duplicate | | Pending |
| TC-22 | Leave filing | Submit leave with valid date range | Request saves with Pending status | | Pending |
| TC-23 | Leave date validation | Set end date before start date | Save is rejected | | Pending |
| TC-24 | HR Staff cannot approve leave | Sign in as HR Staff, select a request | Status control remains disabled and status cannot be changed | | Pending |
| TC-25 | Administrator leave review | Approve/reject a pending request | Status update persists and appears after refresh | | Pending |
| TC-26 | User account management | Administrator creates an HR Staff account | Account is created with PBKDF2 password hash and correct role | | Pending |
| TC-27 | Self-account deletion guard | Administrator selects own account and clicks Delete | Application prevents deleting the currently signed-in account | | Pending |
| TC-28 | Reports | Open each report type | Report rows appear in a non-editable JTable | | Pending |
| TC-29 | CSV export | Export a report to a new CSV file | File is written with headers and correctly escaped values | | Pending |
| TC-30 | Data persistence | Add/update records, close normally, restart application | Saved records remain in Derby database | | Pending |
| TC-31 | Database error handling | Use an invalid database path or make database unavailable | Application shows a startup/operation message rather than silently failing | | Pending |
| TC-32 | Logout | Click Sign out and confirm | Session ends and login screen is shown again | | Pending |

## User acceptance test (UAT)

Ask intended users to evaluate the following statements using a consistent scale (for example, 1 = Strongly Disagree to 5 = Strongly Agree):

1. Labels, buttons, and page navigation are clear.
2. Employee details are easy to enter and update.
3. Search results are easy to understand.
4. Attendance and leave-request screens are straightforward.
5. Error and confirmation messages are understandable.
6. Reports present useful information.
7. Access to Administrator-only functions is appropriately restricted.
8. Overall, the system supports the HR office tasks within the stated scope.

Record the number of respondents, the response scale, item averages, overall mean, comments, and any limitations. Do not report UAT scores before the survey is performed.
