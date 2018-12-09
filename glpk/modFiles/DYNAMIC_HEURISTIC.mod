set N;
/* set of substrate nodes */

set F;
/* set of virtual links */

param bw{u in N, v in N};
/* bandwidth of edge (u,v) */

param hop{u in N, v in N};
/* number of hops between nodes u and v */

param secLink{u in N, v in N};
/* security of edge (u,v) */

param secDemLink{i in F};
/* security demanded by virtual link i */

param fs{i in F};
/* flow start points */

param fe{i in F};
/* flow end points */

param fd{i in F};
/* flow demands */

var fw{i in F, u in N, v in N} >= 0;
/* variable that represents a flow

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

printf{i in F, u in N, v in N} "Variable fw[%s,%s,%s]: %f\n", i,u,v,fw[i,u,v];

printf "Function cost: %f\n", cost;

end;
