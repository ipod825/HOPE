  function r = epsi(n,m,q,f,ws)

    term1=0;
    term2=0;

    sum1=0;
    sum2=0;
    for w=1:ws
      sum1 += nchoosek(n,w)*(1/2+1/2*(1-2*f)^w)^m;
      sum2 += nchoosek(n,w);
    endfor

    term1 = sum1;
    term2 = (q-1-sum2)*(1/2+1/2*(1-2*f)^(ws+1))^m;

    r = (term1+term2)/(q-1);
  endfunction
