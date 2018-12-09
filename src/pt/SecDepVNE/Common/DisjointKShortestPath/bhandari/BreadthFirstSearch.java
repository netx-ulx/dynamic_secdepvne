/**
 * Copyright 2010 Russ Weeks rweeks@newbrightidea.com
 * Licensed under the GNU LGPL
 * License details here: http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package pt.SecDepVNE.Common.DisjointKShortestPath.bhandari;

import pt.SecDepVNE.Common.DisjointKShortestPath.graph.Link;
import pt.SecDepVNE.Common.DisjointKShortestPath.graph.Device;
import pt.SecDepVNE.Common.DisjointKShortestPath.graph.Network;
import pt.SecDepVNE.Common.DisjointKShortestPath.graph.Utils;

import java.util.*;

public class BreadthFirstSearch {

  public static List<Link> findPath(Network nw, Device src, Device dest)
  {
    final PathCalcContext pcc = new PathCalcContext(src);
    return findPath(nw, src, dest, pcc);
  }

  public static List<Link> findPath(Network nw, Device src, Device dest, PathCalcContext pcc)
  {
    final LinkedList<Device> devicesToCheck = new LinkedList<Device>();
    devicesToCheck.add(src);
    while ( !devicesToCheck.isEmpty() )
    {
      Device toCheck = devicesToCheck.pop();
      Set<Link> linksToCheck = nw.getLinksForDevice(toCheck);
      Set<Device> neighboursToCheck = new HashSet<Device>();
      if(linksToCheck == null)
    	  return null;
      for ( Link link: linksToCheck )
      {
        Device otherEnd = Utils.getOtherEndpoint( link, toCheck );
        if ( pcc.updateDistance( otherEnd, link ) && (otherEnd != dest) )
        {
          neighboursToCheck.add(otherEnd);
        }
      }
      devicesToCheck.addAll(neighboursToCheck);
    }
    return pcc.getPathFromPredecessors(dest);
  }
}

