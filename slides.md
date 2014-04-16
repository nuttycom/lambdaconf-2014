% Complexity and Reasoning in Functional Programming
% Kris Nuttycombe<br/> <kris@nutty.land>
% April 19, 2014

# Perfect Software

## "Let me take you on an adventure which will give you superpowers." --@bitemyapp

<div class="notes">

I believe that it is possible to write perfect software. By "perfect", I mean
something very specific: that every state that is representable in source code
is a valid state, and that no invalid states may be obtained by the process of
assembly of the program's components. 

Now, this is a fine sentiment, but it obviously depends very much upon what is
considered to be invalid. A reasonable first estimation of an invalid state is
one that is unexpected; naturally, such a state definitionally is one that the
programmer has not prepared the program to operate correctly from.

</div>

# Programs

  * What is a program?

  * What does it mean for a program to be correct?

  * What space does this leave for errors?

<div class="notes">

A program serves a single purpose: to describe the transformation of
information from one form to another. Most of the time, this transformation
involves many intermediate steps, and may involve various parties along the way
responsible for providing or interpreting the data being transformed. 

You'll note that I say "describes the transformation" rather than "transforms."
Even if a program is never evaluated, it describes a fact about the universe:
that some input data can be combined or reduced to yield some output data. 

Correctness is somewhat more difficult to define. It implies an observer, who
can check the output obtained from the evaluation of a program against the
output which was originally desired, which motivated the creation of the 
program in the first place. Provided every possible combination of inputs,
a correct program is one for which no possible output is undesired.

Of course, I'm sure many of you can immediately think of many outputs of a
program that might not be desired, so we can't be talking about desire in terms
of what makes a person happy; we have to be a little more circumspect and
consider that an error message is a desired thing in response to incorrect
input; "halted" may be a desired state in response to the unavailability of
some resource, and so forth. However, in no case such such a state be
*unexpected*.

</div>

# States 

`True`

<div class="notes">


</div>

# States 

How about these?

  * `x: Boolean`

  * `y: Int`

# States

<div class="notes">

</div>
