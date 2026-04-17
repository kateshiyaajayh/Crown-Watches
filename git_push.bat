@echo off
cd /d "C:\Users\AJAY\Watches"

echo Adding all files...
git add .

echo Committing changes...
git commit -m "Initial Android Project Upload

- Add complete Android project structure
- Update .gitignore with Android-specific patterns
- Create comprehensive README.md with project details
- Ensure no sensitive data is committed

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

echo Checking remote...
git remote -v

echo Pushing to main branch...
git push origin main

echo Done!
