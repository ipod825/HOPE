function result = regf(n,i)

q=2^(i+2);
ws = wstar(n,q);
stepsize = 0.01;
f=0.5;

while(f>0 && epsi(n,i,q,f,ws)< 31/5/(q-1) )
  f -= stepsize;
endwhile

result=f+stepsize;
endfunction