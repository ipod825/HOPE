# Include flags
DAIROOT=/home/mingo/projects/libdai
INC=-I$(DAIROOT)/include
# Library path flags
LIBS=-lgmpxx -lgmp
# Location of libDAI library
LIB=$(DAIROOT)/lib
# Compiler
CC=g++
# Compiler flags
CCFLAGS=-Wno-deprecated -Wall -W -Wextra -fpic -O3 -static $(INC)

all : jt_solve mf_solve trwbp_solve

jt_solve : jt_solve.cpp $(LIB)/libdai.a
	$(CC) $(CCFLAGS) -o$@ $< $(LIB)/libdai.a $(LIBS)
mf_solve : mf_solve.cpp $(LIB)/libdai.a
	$(CC) $(CCFLAGS) -o$@ $< $(LIB)/libdai.a $(LIBS)
trwbp_solve : trwbp_solve.cpp $(LIB)/libdai.a
	$(CC) $(CCFLAGS) -o$@ $< $(LIB)/libdai.a $(LIBS)

# CLEAN
########

.PHONY : clean
clean :
	-rm jt_solve mf_solve trwbp_solve
