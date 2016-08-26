mapotempo-optimizer-jsprit
======

Optimizer-jsprit is the link between <a href="https://github.com/Mapotempo/optimizer-api" target="_blank">Mapotempo-Optimizer API</a> and <a href="https://github.com/Mapotempo/jsprit" target="_blank">Mapotempo-Jsprit</a>.
Look at tag ~~ on each project for the lastest version. Optimize jsprit .jar needs to be generated using the associated custom source of Jsprit.

## Optimizer jsprit require some data :
- Time matrix
- Distance matrix
- Problem xml
- Algorithm xml
- Output file String

## Parameters
- Minmax
- Solve time limit
- Iteration number without improvment
- Iteration number without variation
- Variation threshold
- Threads number
- Prefer to group close jobs

For more details consult the Run.java file

Most of the problem infos must be defined in the problem xml schema defined into the jsprit.io resources
