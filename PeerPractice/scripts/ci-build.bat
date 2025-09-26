@echo off
if exist mvnw (
  set MVN=.\mvnw
) else (
  set MVN=mvn
)
%MVN% -B -Djavafx.platform=%1 test
exit /b %errorlevel%