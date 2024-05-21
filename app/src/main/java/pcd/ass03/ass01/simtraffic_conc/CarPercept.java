package pcd.ass03.ass01.simtraffic_conc;

import java.util.Optional;

import pcd.ass02.ass01.simengine_conc.*;
import pcd.ass03.ass01.simengine_conc.Percept;

/**
 * 
 * Percept for Car Agents
 * 
 * - position on the road
 * - nearest car, if present (distance)
 * - nearest semaphore, if present (distance)
 * 
 */
public record CarPercept(double roadPos, Optional<AbstractCar> nearestCarInFront, Optional<TrafficLightInfo> nearestSem) implements Percept { }