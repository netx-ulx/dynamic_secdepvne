set N;
/* substrate nodes */

/* set E; */
/* substrate links */

set F;
/* virtual links */

param p{i in N};
/* cpu of node i */

param b{u in N, v in N};
/* bandwidth of edge (u,v) */ 

param c{u in N, v in N};
/* cost of edge (u,v) */ 

param alpha{u in N, v in N};
/* edge load balancing weights */

param fs{i in F};
/* flow start points */

param fe{i in F};
/* flow end points */

param fd{i in F};
/* flow demands */

var f{i in F, u in N, v in N} >= 0;
/* flow variable */

minimize cost: (sum{u in N, v in N} ((alpha[u, v] / (b[u, v] + 1E-6)) * sum{i in F} f[i, u, v])); 
/* minimum cost multi-commodity flow */

s.t. capcon{u in N, v in N}: sum{i in F} (f[i,u,v] + f[i, v, u]) <= b[u,v];
/* capacity constraint */

s.t. demsat1{i in F}: sum{w in N} f[i, fs[i], w] - sum{w in N} f[i, w, fs[i]] = fd[i];
s.t. demsat2{i in F}: sum{w in N} f[i, fe[i], w] - sum{w in N} f[i, w, fe[i]] = -fd[i];
/* demand satisfaction */

s.t. flocon{i in F, u in N diff {fs[i], fe[i]}}: sum{w in N} f[i, u, w] - sum{w in N} f[i, w, u] = 0;
/* flow conservation */

solve;

printf "Function cost: %f\n", cost;

#printf{i in F, u in N, v in N} "Variable fw[%s,%d,%d]: %f\n", i, u, v, f[i,u,v];
#if fw[i,u,v] > 0 then printf{i in F, u in N, v in N} "Variable fw[%s,%s,%s]: %f\n", i,u,v,fw[i,u,v];
#printf{i in F, u in N, v in N} "Variable fw[%s,%s,%s]: %f\n", i,u,v,fw[i,u,v];
printf{i in F, u in N, v in N} (if f[i,u,v] > 0 then "Variable fw[%s,%s,%s]: %f\n" else ""), i,u,v,f[i,u,v];
#printf{k in K} "x[%s] = " & (if x[k] < 0 then "?" else "%g"),k, x[k];

end;

