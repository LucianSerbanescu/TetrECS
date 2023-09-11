# COMP2211 SEG

## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://git.soton.ac.uk/hhbl1u21/comp2211-seg-team-17.git
git branch -M main
git push -uf origin main
```

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Automatically merge when pipeline succeeds](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)

***

## Coding standards
- Variables should be explicit (~~var x~~, var landingStripLength)

- Unit tests should be written for each method within a class 

System
```
public class myClass {
    private readonly int _myField;

    public myClass() {
        myField = 0;
    }

    public void incrementMyField() {
        myField++;
    }

    public int getMyField() {
        return _myField;
    }
}
```

TestBaseClass
```
public class myClassTests {
    protected readonly myClass;

    public myClassTests() {
        myClass = new myClass();
    }
}
```

SystemUnderTest
```
public class incrementMyField : myClassTests {
    [test]
    public void incrementsMyField_GivenMethodIsCalled() {
        //Arrange
        var expectedValue = 1;

        //Act
        myClass.incrementMyField();

        //Assert
        myClass.getMyField().ShouldBeEqualTo(expectedValue);
    }
}
```

- Unit test method names should be readable SUT_DOES_GIVENCONDITION

## Branching Strategy
**main** - Feature branches will be based off of this

**feature branch** - Feature branches should be for **ONE** user story feature/FEATURENAME-STORYNAME
e.g. feature/myClass-AddIncrementMethod

for bugs - Bugfix/FEATURENAME-BUGNAME
e.g bugfix/myClass-MethodNotIncrementing

feature branch should require 1-2 code reviews (pull request review) before merging into main.

merges into **main** will trigger CI/CD pipeline.

## CI/CD pipeline
*docker can help you with building an image with all dependecies needed*

Stage - Set Build number  
Stage - Build Image/App  
&emsp;Job - Build Image  
&emsp;Job - Run Unit Tests  
&emsp;Job - Publish image  
Stage - Deploy to environments

## Commits
- Commits should be broken down into many small commits so it is easier to review code e.g. Created class, added x functionality, wrote tests for y
- Commit messages should be descriptive but concise

## PRS
- When creating prs make sure you link the story in the pr message so reviewers have context
