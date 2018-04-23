/*
 * Copyright (C) 2016
 * Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
 * Institut fuer Verkehrssystemtechnik
 * 
 * German Aerospace Center
 * Institute of Transportation Systems
 * 
 */
package de.dlr.ts.lisum.lisa;

import de.dlr.ts.commons.logger.DLRLogger;
import de.dlr.ts.lisum.exceptions.LisaRESTfulServerNotFoundException;
import de.dlr.ts.lisum.interfaces.CityInterface;
import de.dlr.ts.lisum.interfaces.ControlUnitInterface;
import de.dlr.ts.lisum.simulation.Simulation;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author @author <a href="mailto:maximiliano.bottazzi@dlr.de">Maximiliano Bottazzi</a>
 */
public class Lisa implements CityInterface
{    
    private final LisaCommands lisaCommands;    
    private final LisaControlUnits lisaControlUnits = new LisaControlUnits();    
    private File lisaDirectory;
   
    
    /**
     * 
     * @param simulation
     */
    private Lisa()
    {
        lisaCommands = new LisaCommands();
    }

    @Override
    public ControlUnitInterface[] getControlUnits()
    {
        return lisaControlUnits.getControlUnits();
    }
    
    /**
     * 
     * @return 
     */
    public static CityInterface create()
    {
        return new Lisa();
    }
    
    
    /**
     * Looks for xml files in the Lisa directory.      
     * Each of those files represent Control units and contain information related to them.
     * This method loads this information and communicates the Lisa server the data directory.
     * 
     * @param lisaDirectory
     */
    @Override
    public void load(File lisaDirectory)
    {
        DLRLogger.config(this, "Loading directory " + lisaDirectory);
                
        if(!lisaDirectory.exists())
        {
            DLRLogger.severe(this, lisaDirectory + " could not be found");
            return;
        }
        
        this.lisaDirectory = lisaDirectory;
        
        LisaConfigurationFiles configurationFiles = new LisaConfigurationFiles();
        configurationFiles.load(lisaDirectory);        
        
        lisaControlUnits.load(configurationFiles, lisaCommands);
    }
    
    /**
     *     
     * @return 
     */
    @Override
    public Simulation.InitBeforePlayResponse initBeforePlay()
    {
        try {
            lisaCommands.setDataDir(lisaDirectory);
            lisaControlUnits.initBeforePlay();                        
        } catch (LisaRESTfulServerNotFoundException ex) {
            Logger.getLogger(Lisa.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Simulation.InitBeforePlayResponse.OK;
    }

    /**
     * 
     * @param simulationTime
     */
    @Override
    public void executeSimulationStep(long simulationTime)
    {
        
        try
        {
            lisaControlUnits.executeSimulationStep(simulationTime);
        } catch (LisaRESTfulServerNotFoundException ex)
        {
            ex.printStackTrace(System.out);
        }
        
    }

    /**
     * 
     * @param name
     * @return 
     */
    @Override
    public ControlUnitInterface getControlUnit(String name)
    {
        return this.lisaControlUnits.getControlUnit(name);
    }
    
}
