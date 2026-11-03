package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.AddressResult;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Service;

@Service
public class CoordinateService {

    private final CoordinateTransform transform;

    public CoordinateService() {
        CRSFactory crsFactory = new CRSFactory();

        CoordinateReferenceSystem sourceSystem =
                crsFactory.createFromName("EPSG:25832");

        CoordinateReferenceSystem targetSystem =
                crsFactory.createFromName("EPSG:4326");

        CoordinateTransformFactory transformFactory =
                new CoordinateTransformFactory();

        this.transform = transformFactory.createTransform(
                sourceSystem,
                targetSystem
        );
    }

    public AddressResult convert(
            String address,
            double x,
            double y
    ) {
        ProjCoordinate sourceCoordinate =
                new ProjCoordinate(x, y);

        ProjCoordinate targetCoordinate =
                new ProjCoordinate();

        transform.transform(
                sourceCoordinate,
                targetCoordinate
        );

        return new AddressResult(
                address,
                targetCoordinate.y,
                targetCoordinate.x
        );
    }
}