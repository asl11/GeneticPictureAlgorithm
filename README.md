<img align=right src="https://brand.rice.edu/themes/custom/adm_rice/assets/img/Rice_University_Horizontal_Blue.svg" width=400>

# Comp215, Fall 2019
## Week 14/15: Pretty Pictures (Week 3)

This three-week long project will take place in 
[src/main/java/edu/rice/prettypictures](/../../tree/master/src/main/java/edu/rice/prettypictures).

## Student Information
Please edit _README.md_ and replace your instructor's name and NetID with your own:
(In future weeks, we'll have more things for you to do here.)

_Student 1 name_: Joshua Dong

_Student 1 NetID_: jd54

_Student 2 name_: Alex Li

_Student 2 NetID_: asl11

Your NetID is typically your initials and a numeric digit. That's
what we need here.

_If you contacted us in advance about the late policy and we approved a late submission,
please cut-and-paste the text from that email here._

## Part3 ToDo Items

To make your graders lives easier, please answer these questions:

1) Did you implement TEST 3 (mutants)? ( yes )
2) Did you implement TEST 4 (crossbreeding)? ( yes )
3) Where did you put your "ten favorite images" and their JSON genotypes? ( "favoritepictures.json" ) same directory as readme
4) Where is the unit test that exercises your GeneTree lenses? ( directory/name/here )
5) Where is the unit test that exercises your GeneTree mutation? ( directory/name/here )
6) Where is the unit test that exercises your GeneTree crossbreeding? ( directory/name/here )
7) Where are the unit tests that exercises your multi-generation database JSON loading and saving? ( directory/name/here )
8) Where are the unit tests that exercises your multi-generation database's ability to fetch individual genes and create new generations? ( directory / name / here )

## Q&A

Please answer these questions with one or two short sentences each. Note that each partner will
answer these separately.

### Student 1

**GeneTree / RandomGeneTree**
- What were some similarities in how you defined your GeneTree last week and how our reference implementation works?

  (Your answer.)
  
- What were some significant differences in how you defined your GeneTree last week?

  (Your answer.)
  
**Crossbreeding**
- Last week, we asked you to think about how you would *crossbreed* two parent gene-trees together, getting a
  new child gene-tree. How does your idea from last week compare to our lens-based approach this week?

  (Your answer.)

### Student 2 - Alex Li

**GeneTree / RandomGeneTree**
- What were some similarities in how you defined your GeneTree last week and how our reference implementation works?

  We both have extensive error handling and store the children and gene seperately in fields in the class
  
- What were some significant differences in how you defined your GeneTree last week?

  My GeneTree class last week defined the children as an option of genetrees, instead of a sequence of genetrees.
  Also, my genetree gene was just a string and was much messier than making it an allele
  
**Crossbreeding**
- Last week, we asked you to think about how you would *crossbreed* two parent gene-trees together, getting a
  new child gene-tree. How does your idea from last week compare to our lens-based approach this week?

  It was similar in that I said that we would just take a random part of one tree and put it onto a random part of another.
  The lenses make this process much simpler and easier to test. 

## Cool points

If you did something cool that you want to call to our attention, write it here.
