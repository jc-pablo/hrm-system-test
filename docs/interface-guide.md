# Interface guide

The interface uses a light workspace, dark readable text, white content cards, a persistent dark-navy sidebar, and tables with visible headers and row selection. Sidebar navigation buttons have white text on navy, dark blue hover/pressed states, and a brighter selected state with a pale-blue indicator. The buttons use explicit Swing button UI styling so the operating-system look and feel cannot replace the foreground/background pair with lower-contrast colors.

1. **Login:** the username and password fields are empty on launch. No demo credentials are shown under the fields. Show password is optional. Clear/Reset empties both fields. Exit closes the program after confirmation.
2. **Dashboard:** displays employee, active employee, department, pending-leave, attendance, and recent-employee summaries.
3. **Employees:** required fields are employee number, names, birth date, gender, hire date, status, department, and position. Email and time/date formats are validated where applicable.
4. **Departments / Positions:** create and maintain reference data used by employee forms.
5. **Attendance:** record employee, date, time in/out and status. One record per employee per date is enforced by Derby.
6. **Leave Requests:** HR Staff can file and edit request details but cannot change the review status. Administrator can approve or reject.
7. **User Accounts:** available only to Administrator. Password is set during account creation; leaving Password blank during an edit keeps the existing hash.
8. **Reports:** choose Employee Directory, Department Headcount, Attendance Register, Leave Register, or Pending Leave Requests. Refresh reloads live data; Export CSV saves the displayed rows.
9. **Sign out:** prompts for confirmation, closes the authenticated workspace, and returns to the login form.

Demo credentials are listed in the project README for testing, not displayed in the login interface. The panels use Swing controls and layouts; they are constructed in Java code rather than NetBeans visual `.form` files.
