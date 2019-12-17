package at.austriapro.ebinterface.ubl.from;

import javax.annotation.Nonnull;

import com.helger.commons.functional.IFunction;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.PeppolIdentifierFactory;
import com.helger.peppolid.peppol.process.PredefinedProcessIdentifierManager;

/**
 * A utility interface to resolve a profile ID to a parsed process identifier
 * 
 * @author Philip Helger
 */
public interface IProfileIDResolver extends IFunction <String, IProcessIdentifier>
{
  /* empty */
  @Nonnull
  static IProfileIDResolver getDefault ()
  {
    return sProfileID -> {
      IProcessIdentifier aProcID = PredefinedProcessIdentifierManager.getProcessIdentifierOfID (sProfileID);
      if (aProcID == null)
      {
        // Parse basically
        aProcID = PeppolIdentifierFactory.INSTANCE.parseProcessIdentifier (sProfileID);
      }
      return aProcID;
    };
  }
}
