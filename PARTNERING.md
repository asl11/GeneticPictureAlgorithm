<img align=right src="https://brand.rice.edu/themes/custom/adm_rice/assets/img/Rice_University_Horizontal_Blue.svg" width=400>

# Comp215 Partnering
Starting in week7, you'll be doing your labs and projects with a partner. 
This post explains how that's going to work. We also have some
[helpful resources](#resources) for you, including YouTube videos.
The Monday lecture (week7a) is going to be a 
50-minute-long demonstration, so you really want to be there.

## How partnerships happen

We'll show up in each lab section with a printout of all the students registered in that section. 
Please be on time. **You may partner with anybody in your lab section, but you must have a different
partner each week**. This is your chance to get to know your classmates. You'll
find everybody has something to add, something to teach you, and something to learn from
you. Dr. Wallach is still friends with some of his lab partners from 30 years ago!

Once you've decided on a partner, we'll check you off on the list. 
Nobody leaves without a partner!
If you fail to arrive at the lab, and you did not let us know
in advance, you will not get the lab checkoff point, and you
may or may not be assigned a partner. See 
["partnering is a privilege"](#partnering-is-a-privilege), below.

## Getting your GitHub repo set up

Once you've decided who your partner is, you and they will do the 
clone link process together. We'll walk you through this in class,
but the idea is that GitHub Classroom allows one of you to declare
a name for your team and then your partner can join your team.
*Your team name must be your two NetIDs glued together.* So if one
of you is `abc1` and the other is `def2`, then your team name should
be `abc1def2`. No spaces. No dashes. No `@rice.edu`. Just the NetIDs.

Before you do anything else, make sure you can both clone and view
your repository, then each of you should edit your name into the 
`README.md` file. Note: several of you have a personal name that
you like to use which is different from the way your name appears
in Canvas. **Please put your names and NetIDs into the README file
*exactly* as they appear in Canvas.** This is essential for us
to make sure we can assign your grade to you.

Do this sequentially, so you can verify that
each of you can do a `commit` and `push`, followed by your partner
doing a `pull` to see your changes. You're verifying two things:

- Changes from one partner become visible to the other partner.

- The "commits" tab, on the GitHub page, shows your individual names
  next to your commits. If this doesn't work, find a labbie or TA
  to help you.

## What about groups of three?

We may end up in a situation with an odd number of students in a
lab section. In those cases, we will build a single group of three
to make sure that nobody is working alone. We will try to ensure
that each of you is only in a group of three at most once in the
semester.

## What if I can't be there for the lab?

If you have an unavoidable time conflict that prevents you from
attending your lab (e.g., you're sick, or you're traveling to some
sort of intercollegiate activity), you may email Dr. Wallach in
advance of your lab section, and identify somebody who is prepared
to be your partner, including working remotely with you during the
week's project. 

## How to partner

There's been a bunch of academic research on what makes for
effective programming partnerships. In particular, *pair programming*,
wherein you and your partner sit side-by-side and work with a single
screen, has been shown to have better outcomes than the two partners
working separately. The concept is that you have a "pilot" and a 
"navigator", where the navigator is looking over the pilot's shoulder,
offering advice, and also doing tasks on the side, such as looking
up the relevant documentation for how an API works.

In some CS classes, they literally ring a bell every ten minutes
and force you to switch roles. We don't do that, but we trust you
to keep these ideas in mind.

You may be tempted to simply divide the work in two pieces. "I'll do 
this, you do that." The problem with this is that neither of you
fully understands the results of your work, and it's particularly 
harder to figure out whose code is broken when the resulting parts
don't properly integrate.

Nonetheless, a one-size-fits-all policy like this is going to be
at times restrictive and unhelpful. There will be plenty of places where it's 
reasonable and appropriate to work apart from one another. Still,
there are several good practices to follow:

- Don't let one partner be "in charge". You're doing this work 
  together. 
- Don't let one partner blast ahead when the other partner "doesn't get it". 
  You need to both get it. 
- Turn off your texting and social media and all that. When you're
  working together, your time is valuable, so you should be 100% focused
  on the work at hand. Take breaks to clear your head, then get back to it.
- *Don't wait until the last minute!* Identify times that you and your
  partner will meet to get the work done.
- Don't just do your part and assume your partner will deliver their part.
  You're both responsible for everything.

## Partnering is a privilege

No matter what we say or do, there will be students in our class
who don't take their responsibilities seriously. They don't respond
to emails or texts. They're chatting away on some sort of social
media when they should be focused on the work at hand. They're
overwhelmed with other classwork and blow off Comp215.

If you find yourself in a situation where your partner is not pulling
their weight, please send an email to Dr. Wallach. We might take a
number of actions, including splitting your team apart, or dividing
the project points in a non-uniform fashion.

*Partnering is a privilege!*
If a student is consistently failing to be a good partner, we may
remove the privilege. The projects get notably more complicated
as we get later into the semester. It's possible to do them
solo, but it's much less fun.

## Feedback forms

After each week's project, we'll post a form where you can
answer some quick questions about your partnering experience.
This is optional, but it's useful. For the students who
consistently get the best ratings from their partners,
we will have some modest prizes to give out at the end of
the semester. And for the students who consistently get
low ratings from their partners, we may remind them privately
about our expectations.

# Dealing with Git

Here are some general coding rules that will help you avoid merge conflicts:

- Make sure you and your partner agree on which parts of which
  files you're each working on. If you're working in different files,
  or if you're at least working on different parts of the same
  file, you're likely to avoid conflicts. If you're working
  physically apart, you may wish to have a text chat open
  somewhere so you can say "I'm working on Foo.java", so
  your partner will know to leave it alone.

- Always do a `git pull` (a.k.a. *VCS* → *Update Project*) before
  making any changes.
  
- When you think you're ready to commit, run the auto-indenter
  and make sure you're getting a clean report from the autograder about
  warnings and such. You don't want to commit code that's broken, nor
  do you want to have a commit where the
  code is really unchanged, but indentation
  changes make it appear as if there's a big difference.

- Do *another* `git pull` before committing your changes. This
  will merge in any changes from your partner.
  
- Finally, do a `git commit` (a.k.a. *VCS* → *Commit*) and push
  your changes to the server.
  
- If you did a `commit` and didn't `push`, you can explicitly
  run *VCS* → *Git...* → *Push* to make sure that your commit(s)
  go from your machine to the GitHub server.
 
And some useful don'ts:
- Don't do Git branches. Keep all your work on the `master` branch.
  When you work with larger groups on longer timelines, branching
  is appropriate, but here it's just going to get confusing.
  
- Don't do Git rebasing. It's incredibly confusing to rebase.
  Just use the branch merging facilities built into IntelliJ.
  
- Try to avoid the command-line Git utilities. IntelliJ's built-in
  facilities are much more helpful when dealing with merge conflicts.
  
And, if all else fails:
- Quit IntelliJ, go to your projects directory, and rename the project
  to something else (e.g., change `comp215-week07-parsing-abc1def2` to 
  `comp215-week07-parsing-abc1def2-backup`), then restart IntelliJ and
  reimport the project from GitHub. At that point, you can manually copy
  files from your "backup" to your new project and do a fresh commit.

## Resources

There are tons of resources that talk about the
mechanics of collaborating with Git, GitHub,
and IntelliJ. Here are some that we found useful:

- IntelliJ documentation for dealing with merge conflicts
(https://www.jetbrains.com/help/idea/resolve-conflicts.html)

- IntelliJ documentation for undoing a change
(https://www.jetbrains.com/help/idea/undo-changes.html)

- IntelliJ documentation for reviewing the project history;
 e.g., to see what your partner did
 (https://www.jetbrains.com/help/idea/investigate-changes.html)
 
- An independent walkthrough with lots of screenshots for
  different ways of merging (https://zippyzsquirrel.github.io/squirrel-u/1_SquirrelU/4_GitHub/2_basicConcepts/3_fetchMergePull/)

- A 12-minute YouTube video that walks you through merging changes
  (https://www.youtube.com/watch?v=RxunYSzMNKM)
  
- a 4-minute YouTube video that demonstrates some advanced Git
  features in IntelliJ
  (https://www.youtube.com/watch?v=pQCekxV-xuk)

