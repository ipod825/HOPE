function result = wstar(n,q)

sum=0;
j=0;
do
  j++;
  sum += nchoosek(n,j);
until(sum >= q || j==n)
result=j-1;
endfunction
