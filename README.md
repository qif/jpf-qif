jpf-qif
=========
Symbolic Quantitative Information Flow Analysis for Java Bytecode

Project pages
----

http://babelfish.arc.nasa.gov/trac/jpf/wiki/summer-projects/2012-qif  

http://www.google-melange.com/gsoc/project/details/google/gsoc2012/qsphan/5698390809640960

Abstract
----

Computer systems in the real world are never 100% secure, so we need to measure how secure they are. As a simple example, suppose an attacker tries to guess a password: if his guess is correct, he can obtain all the information; otherwise he can still learn that the password is not the same as the previous try, so his search space is narrowed. In either the cases, the password checking program does leak some information. The aim of this project is to use JPF to quantify leakage of confidential information in Java programs. 

Paper
----
[Symbolic Quantitative Information Flow](http://qsphan.github.io/papers/paper1.pdf). **JPF** 2012.  
Quoc-Sang Phan, Pasquale Malacaria, Oksana Tkachuk, and Corina S. Pasareanu. 

Install
----

The following instructions is for people who have no idea about Java Pathfinder. Users need to have Apache Ant installed in their computer.

1. Create a new folder "**jpf**" where you want to put Java Pathfinder and its extensions. 
   For example, the folder in my computer is: 
        /homes/qsp30/Programs/jpf

2. Get jpf-core, build and test it

        cd /homes/qsp30/Programs/jpf  # replace with your jpf folder  
        hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core -r v6  
        jpf-core/bin/ant test

3. Get jpf-symbc, build and test it

        hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-symbc -r v6
        cd jpf-symbc
        ant test

4. Get jpf-qif, build it

        git clone https://github.com/dark2bright/jpf-qif.git
        cd jpf-qif
        ant build

5. Create a site.properties file under ${user.home}/.jpf, modify its content as follows:

        jpf-home = /homes/qsp30/Programs/jpf # replace with your jpf folder  
        jpf-core = ${jpf-home}/jpf-core   
        jpf-symbc = ${jpf-home}/jpf-symbc  
        jpf-qif = ${jpf-home}/jpf-qif  
        extensions+=,${jpf-core}   
        extensions+=,${jpf-symbc}  
        extensions+=,${jpf-qif}  
   
   Consult here for more details:  
   
     http://babelfish.arc.nasa.gov/trac/jpf/wiki/install/site-properties

   At the end of this step, your jpf folder should be as follows:
   
        ${user.home}/.jpf/site.properties  
        jpf/jpf-core  
        jpf/jpf-symbc  
        jpf/jpf-qif  

6. Setup environment variables. For Linux, edit .bashrc file as follows:

        export JPF_CORE_HOME=/homes/qsp30/Programs/jpf/jpf-core   
        export JPF_SYMBC_HOME=/homes/qsp30/Programs/jpf/jpf-symbc   
        export JPF_QIF_HOME=/homes/qsp30/Programs/jpf/jpf-qif  

   jpf-qif uses **jpf-symbc** with **cvc3** solver. Therefore we need to setup:
   
        export LD_LIBRARY_PATH=$JPF_SYMBC_HOME/lib:$LD_LIBRARY_PATH  
    
   Please be aware that the cvc3 solver under jpf-symbc is for 32-bit architecture, if you use 64-bit OS, then you need to install cvc3 yourself:
    
    http://www.cs.nyu.edu/acsys/cvc3/

7. To run the SQIF-SE in the JPF 2012 paper, suppose you are in jpf-qif folder:

        bin/qif src/examples/ImplicitFlow.jpf

   You should have the following result:

        >>> Quantitative Information Flow Analysis:  
        Number of possible outputs: 7  
        Maximum of leakage: 2.807354922057604

   There are other examples in the same folder.

8. The BitVector method needs to be run from Eclipse

   The main class is Analyzer and the running configuration contains two parameters: the class name and the method. For example: "plas.ImplicitFlow func" or "plas.SanityCheck1 func"

   Running "plas.SanityCheck1 func" will result a CNF formula in DIMACS format. You need to use relsat to count the number of solutions for this formula
   
   http://code.google.com/p/relsat/
   
   For the SanityCheck1 example, **relsat** will return N = 16. Hence, log(N) = 4 is the maximum leaks of confidential information. Please consult the JPF 2012 paper for more theoretical background.  
   
    http://dl.acm.org/citation.cfm?id=2382791

   If you experience any difficulty installing **jpf-qif**, please drop me a line at **q.phan AT qmul.ac.uk**. I will reply with my best effort.
