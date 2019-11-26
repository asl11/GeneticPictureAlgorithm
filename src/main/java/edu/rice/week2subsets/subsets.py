#
# This code is part of Rice Comp215 and is made available for your
# use as a student in Comp215. You are specifically forbidden from
# posting this code online in a public fashion (e.g., on a public
# GitHub repository) or otherwise making it, or any derivative of it,
# available to future Comp215 students. Violations of this rule are
# considered Honor Code violations and will result in your being
# reported to the Honor Council, even after you've completed the
# class, and will result in retroactive reductions to your grade. For
# additional details, please see the Comp215 course syllabus.
#

import time


def subsets(input):
    if not input:
        return [[]]
    else:
        children = subsets(input[1:])
        children_plus = [[input[0]] + x for x in children]
        return children + children_plus


print("Python subset performance test")

MAX = 100
big_list = range(MAX)

for i in big_list:
    start = time.time()
    results = subsets(big_list[0:i])
    end = time.time()
    delta = end - start

    print("%d,%g" % (i, delta))
    if delta > 3.0:
        exit(0)
