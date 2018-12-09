set N;
/* substrate nodes */

set F;
/* virtual links */

param bw{u in N, v in N};
/* bandwidth of edge (u,v) */ 

param hop{u in N, v in N};
/* number of hops between the substrate nodes (u,v) */

param secLink{u in N, v in N};
/* security of the substrat link */

param secDemLink{i in F};
/* security demanded by virtual link */

param fs{i in F};
/* flow start points */

param fe{i in F};
/* flow end points */

param fd{i in F};
/* flow demands */

var fw{i in F, u in N, v in N} >= 0;
/* flow variable */


#minimize cost: (sum{u in N, v in N} ((alpha[u, v] / (b[u, v] + 1E-6)) * sum{i in F} f[i, u, v])); 
#minimize cost: (sum{u in N, v in N}  sum{i in F} f[i, u, v] * secLink[u, v]) +1;
#minimize cost: (sum{u in N, v in N}  sum{i in F} f[i, u, v] * secLink[u, v] * c[u, v] + hop[u, v]);
#minimize cost: (sum{u in N, v in N}  sum{i in F} f[i, u, v] * secLink[u, v] * c[u, v])+ (sum{u in N, v in N} hop[u, v]);
#minimize cost: (sum{u in N, v in N} hop[u, v] *  c[u, v]);
minimize cost: (sum{u in N, v in N}  sum{i in F} fw[i, u, v] * secLink[u, v]);
/* minimum cost multi-commodity flow */

s.t. capcon{u in N, v in N}: sum{i in F} (fw[i,u,v] + fw[i, v, u]) <= bw[u,v];
/* capacity constraint */

s.t. demsat1{i in F}: sum{w in N} fw[i, fs[i], w] - sum{w in N} fw[i, w, fs[i]] = fd[i];
s.t. demsat2{i in F}: sum{w in N} fw[i, fe[i], w] - sum{w in N} fw[i, w, fe[i]] = -fd[i];
/* demand satisfaction */

s.t. flocon{i in F, u in N diff {fs[i], fe[i]}}: sum{w in N} fw[i, u, w] - sum{w in N} fw[i, w, u] = 0;
/* flow conservation */

solve;

#printf{i in F, u in N, v in N} "Variable fw[%s,%s,%s]: %f\n", i,u,v,fw[i,u,v];
#if fw[i,u,v] > 0 then printf{i in F, u in N, v in N} "Variable fw[%s,%s,%s]: %f\n", i,u,v,fw[i,u,v];
#printf{i in F, u in N, v in N} "Variable fw[%s,%s,%s]: %f\n", i,u,v,fw[i,u,v];
printf{i in F, u in N, v in N} (if fw[i,u,v] > 0 then "Variable fw[%s,%s,%s]: %f\n" else ""), i,u,v,fw[i,u,v];
#printf{k in K} "x[%s] = " & (if x[k] < 0 then "?" else "%g"),k, x[k];

printf "Function cost: %f\n", cost;

end;
