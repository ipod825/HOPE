function fs = allf(n)

fs=zeros(1,n);
for i=1:n
  f = regf(n,i)
  fs(1,i)=f;
endfor
endfunction
