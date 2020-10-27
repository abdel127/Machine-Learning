# Partnering Project

The problem I am trying to tackle is a more complex form of a constraint satisfaction problem (CSP). CSP refers to a problem that has a set of variables X = {X1, ..., Xn} whose value (picked from a set D = {D1, ..., Dn} of respective domain of values) must satisfy constraints and limitations found in the set C. The most common CSPs are map coloring problems and the N-queens puzzle. Here I will be looking at a CSP whose variables are to be assigned a pair of
values. More specifically, the problem is inspired by my ballroom dance team which has 56 dancers (28 leaders/males, 28 followers/females), and the goal is
to give each dancer two partners from the opposite group. Each dancer will be paired with exactly two dancers, and the complexity comes from the next
two constraints: each dancer has their own preferences/constraints of who they want to dance with, and who they don’t want to dance with; each dancer dances
at a specific skill level, this has to be taken into account so there is not a skill difference bigger than two levels in one partnership. A partnership is a two way
relationship, meaning if {X1} is partnered with (has a value of) {X2}, then {X2} is also partnered with {X1}.

### Full research paper available in the PDF.
